package br.com.achadoseperdidos.controller;

import br.com.achadoseperdidos.model.LostFoundPost;
import br.com.achadoseperdidos.service.ImageProcessor;
import br.com.achadoseperdidos.service.PostService;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import jakarta.servlet.http.HttpSession;

@Controller
public class PostController {
    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;
    private static final List<StateOption> STATES = List.of(
            new StateOption("AC", "Acre"), new StateOption("AL", "Alagoas"),
            new StateOption("AP", "Amapá"), new StateOption("AM", "Amazonas"),
            new StateOption("BA", "Bahia"), new StateOption("CE", "Ceará"),
            new StateOption("DF", "Distrito Federal"), new StateOption("ES", "Espírito Santo"),
            new StateOption("GO", "Goiás"), new StateOption("MA", "Maranhão"),
            new StateOption("MT", "Mato Grosso"), new StateOption("MS", "Mato Grosso do Sul"),
            new StateOption("MG", "Minas Gerais"), new StateOption("PA", "Pará"),
            new StateOption("PB", "Paraíba"), new StateOption("PR", "Paraná"),
            new StateOption("PE", "Pernambuco"), new StateOption("PI", "Piauí"),
            new StateOption("RJ", "Rio de Janeiro"), new StateOption("RN", "Rio Grande do Norte"),
            new StateOption("RS", "Rio Grande do Sul"), new StateOption("RO", "Rondônia"),
            new StateOption("RR", "Roraima"), new StateOption("SC", "Santa Catarina"),
            new StateOption("SP", "São Paulo"), new StateOption("SE", "Sergipe"),
            new StateOption("TO", "Tocantins"));

    private final PostService postService;
    private final ImageProcessor imageProcessor;

    public PostController(PostService postService, ImageProcessor imageProcessor) {
        this.postService = postService;
        this.imageProcessor = imageProcessor;
    }

    @GetMapping("/")
    public String index(
            @RequestParam(defaultValue = "todos") String status,
            @RequestParam(defaultValue = "") String state,
            @RequestParam(defaultValue = "") String city,
            @RequestParam(defaultValue = "") String neighborhood,
            @RequestParam(defaultValue = "") String q,
                       @RequestParam(required = false) Long openComments,
            Model model,
            HttpSession session) {

        if (session.getAttribute("usuarioId") == null) {
            return "redirect:/login";
        }
        String activeStatus = List.of("todos", "perdido", "encontrado").contains(status) ? status : "todos";
        String activeState = STATES.stream().anyMatch(option -> option.getCode().equals(state)) ? state : "";
        List<String> cities = postService.findCities(activeState);
        String activeCity = cities.stream().anyMatch(option -> option.equalsIgnoreCase(city)) ? city : "";
        List<String> neighborhoods = postService.findNeighborhoods(activeState, activeCity);
        String activeNeighborhood = neighborhoods.stream()
                .anyMatch(option -> option.equalsIgnoreCase(neighborhood)) ? neighborhood : "";

        model.addAttribute("posts", postService.findPosts(
                activeStatus, activeState, activeCity, activeNeighborhood, q));
        model.addAttribute("states", STATES);
        model.addAttribute("cities", cities);
        model.addAttribute("neighborhoods", neighborhoods);
        model.addAttribute("selectedStatus", activeStatus);
        model.addAttribute("selectedState", activeState);
        model.addAttribute("selectedCity", activeCity);
        model.addAttribute("selectedNeighborhood", activeNeighborhood);
        model.addAttribute("query", q);
        model.addAttribute("openComments", openComments);
        return "index";
    }

    @GetMapping("/publicar")
public String publishPage(Model model, HttpSession session) {

    if (session.getAttribute("usuarioId") == null) {
        return "redirect:/login";
    }

    model.addAttribute("states", STATES);
    return "publish";
}

    @PostMapping("/posts")
    public String createPost(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String location,
            @RequestParam String state,
            @RequestParam String city,
            @RequestParam String neighborhood,
            @RequestParam String status,
            @RequestParam(defaultValue = "contain") String imageFit,
            @RequestParam(defaultValue = "1") double zoom,
        @RequestParam(required = false) MultipartFile image,
RedirectAttributes redirectAttributes,
HttpSession session) {

    if (session.getAttribute("usuarioId") == null) {
        return "redirect:/login";
    }
        String cleanTitle = title.trim();
        String cleanDescription = description.trim();
        String cleanLocation = location.trim();
        String cleanCity = city.trim();
        String cleanNeighborhood = neighborhood.trim();

        if (!StringUtils.hasText(cleanTitle) || cleanTitle.length() > 70
                || !StringUtils.hasText(cleanDescription) || cleanDescription.length() > 300
                || !StringUtils.hasText(cleanLocation) || cleanLocation.length() > 60
                || !StringUtils.hasText(cleanCity) || cleanCity.length() > 60
                || !StringUtils.hasText(cleanNeighborhood) || cleanNeighborhood.length() > 60) {
            redirectAttributes.addFlashAttribute("error", "Preencha os campos corretamente.");
            return "redirect:/publicar";
        }
        if (STATES.stream().noneMatch(option -> option.getCode().equals(state))
                || !List.of("perdido", "encontrado").contains(status)) {
            redirectAttributes.addFlashAttribute("error", "Selecione um estado e uma situação válidos.");
            return "redirect:/publicar";
        }
        if (!"contain".equals(imageFit) && !"cover".equals(imageFit)) {
            redirectAttributes.addFlashAttribute("error", "Selecione um ajuste de foto válido.");
            return "redirect:/publicar";
        }
        if (zoom < 1 || zoom > 2.5) {
            redirectAttributes.addFlashAttribute("error", "O zoom deve estar entre 1 e 2,5.");
            return "redirect:/publicar";
        }

        byte[] processedImage = new byte[0];
        if (image != null && !image.isEmpty()) {
            if (image.getSize() > MAX_IMAGE_SIZE) {
                redirectAttributes.addFlashAttribute("error", "A foto deve ter no máximo 5 MB.");
                return "redirect:/publicar";
            }
            String contentType = image.getContentType();
            if (!MediaType.IMAGE_JPEG_VALUE.equals(contentType) && !MediaType.IMAGE_PNG_VALUE.equals(contentType)) {
                redirectAttributes.addFlashAttribute("error", "Envie uma imagem JPG ou PNG.");
                return "redirect:/publicar";
            }
            try {
                processedImage = imageProcessor.resizeAndCrop(image.getBytes(), imageFit, zoom);
            } catch (IOException | IllegalArgumentException exception) {
                redirectAttributes.addFlashAttribute("error", exception.getMessage());
                return "redirect:/publicar";
            }
        }

        postService.createPost(cleanTitle, cleanDescription, cleanLocation, state, cleanCity,
                cleanNeighborhood, status, processedImage);
        redirectAttributes.addFlashAttribute("success", "Publicação adicionada. Obrigado por ajudar!");
        return "redirect:/";
    }

    @PostMapping("/posts/{id}/comments")
    public String addComment(
            @PathVariable long id,
            @RequestParam String text,
          RedirectAttributes redirectAttributes,
HttpSession session) {
    if (session.getAttribute("usuarioId") == null) {
    return "redirect:/login";
}
        String cleanText = text.trim();
        if (!StringUtils.hasText(cleanText) || cleanText.length() > 180) {
            redirectAttributes.addFlashAttribute("error", "O comentário deve ter entre 1 e 180 caracteres.");
            return "redirect:/?openComments=" + id + "#post-" + id;
        }
        if (!postService.addComment(id, cleanText)) {
            redirectAttributes.addFlashAttribute("error", "A publicação não foi encontrada.");
            return "redirect:/";
        }
        return "redirect:/?openComments=" + id + "#post-" + id;
    }

   @GetMapping("/images/{id}")
public ResponseEntity<byte[]> image(
        @PathVariable long id,
        HttpSession session) {

    if (session.getAttribute("usuarioId") == null) {
        return ResponseEntity.status(401).build();
    }
        Optional<LostFoundPost> post = postService.findById(id);
        if (post.isEmpty() || !post.get().hasImage()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .cacheControl(CacheControl.noCache())
                .header("X-Content-Type-Options", "nosniff")
                .body(post.get().getImage());
    }

    public static class StateOption {
        private final String code;
        private final String name;

        public StateOption(String code, String name) {
            this.code = code;
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }
    }
}
