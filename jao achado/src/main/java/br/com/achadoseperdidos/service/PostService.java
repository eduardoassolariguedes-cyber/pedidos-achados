
package br.com.achadoseperdidos.service;

import br.com.achadoseperdidos.model.LostFoundPost;
import br.com.achadoseperdidos.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@Transactional
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public List<LostFoundPost> findPosts(
            String status,
            String state,
            String city,
            String neighborhood,
            String query) {

        String normalizedQuery = normalize(query);

        return postRepository.findAll().stream()
                .filter(post -> status == null || status.isBlank()
                        || "todos".equals(status)
                        || post.getStatus().equals(status))
                .filter(post -> matches(post.getState(), state))
                .filter(post -> matches(post.getCity(), city))
                .filter(post -> matches(post.getNeighborhood(), neighborhood))
                .filter(post -> normalizedQuery.isBlank()
                        || normalize(String.join(" ",
                        post.getTitle(),
                        post.getDescription(),
                        post.getLocation(),
                        post.getState(),
                        post.getCity(),
                        post.getNeighborhood())).contains(normalizedQuery))
                .sorted(Comparator.comparingLong(
        (LostFoundPost post) -> post.getId()).reversed())
                .toList();
    }

    public List<String> findCities(String state) {
        return postRepository.findAll().stream()
                .filter(post -> matches(post.getState(), state))
                .map(LostFoundPost::getCity)
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    public List<String> findNeighborhoods(String state, String city) {
        return postRepository.findAll().stream()
                .filter(post -> matches(post.getState(), state))
                .filter(post -> matches(post.getCity(), city))
                .map(LostFoundPost::getNeighborhood)
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    public LostFoundPost createPost(
            String title,
            String description,
            String location,
            String state,
            String city,
            String neighborhood,
            String status,
            byte[] image) {

        String emoji = "perdido".equals(status) ? "🔍" : "📦";
        String tone = "perdido".equals(status) ? "tone-peach" : "tone-green";

        LostFoundPost post = new LostFoundPost(
                0L,
                "Você",
                "EU",
                title,
                description,
                location,
                state,
                city,
                neighborhood,
                status,
                emoji,
                tone,
                "agora",
                image,
                List.of()
        );

        return postRepository.save(post);
    }

    public Optional<LostFoundPost> findById(long id) {
        return postRepository.findById(id);
    }

    public boolean addComment(long id, String text) {
        Optional<LostFoundPost> post = postRepository.findById(id);

        if (post.isEmpty()) {
            return false;
        }

        post.get().addComment("Você", text);
        postRepository.save(post.get());

        return true;
    }

    private boolean matches(String actual, String expected) {
        return expected == null
                || expected.isBlank()
                || normalize(actual).equals(normalize(expected));
    }

    private String normalize(String value) {
        return value == null
                ? ""
                : value.trim().toLowerCase(Locale.forLanguageTag("pt-BR"));
    }
}
