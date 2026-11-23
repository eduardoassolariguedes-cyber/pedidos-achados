
package br.com.achadoseperdidos.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
public class LostFoundPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String author;
    private String initials;
    private String title;

    @Column(length = 300)
    private String description;

    private String location;
    private String state;
    private String city;
    private String neighborhood;
    private String status;
    private String emoji;
    private String tone;
    private String createdAt;

    @Lob
    private byte[] image;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    public LostFoundPost() {
    }

    public LostFoundPost(
            long id,
            String author,
            String initials,
            String title,
            String description,
            String location,
            String state,
            String city,
            String neighborhood,
            String status,
            String emoji,
            String tone,
            String createdAt,
            byte[] image,
            List<Comment> initialComments) {

        this.id = id <= 0 ? null : id;
        this.author = author;
        this.initials = initials;
        this.title = title;
        this.description = description;
        this.location = location;
        this.state = state;
        this.city = city;
        this.neighborhood = neighborhood;
        this.status = status;
        this.emoji = emoji;
        this.tone = tone;
        this.createdAt = createdAt;
        this.image = image == null ? new byte[0] : image.clone();

        if (initialComments != null) {
            for (Comment comment : initialComments) {
                addComment(comment);
            }
        }
    }

    public void addComment(String author, String text) {
        addComment(new Comment(author, text));
    }

    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setPost(this);
    }

    public boolean hasImage() {
        return image != null && image.length > 0;
    }

    public Long getId() { return id; }
    public String getAuthor() { return author; }
    public String getInitials() { return initials; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public String getState() { return state; }
    public String getCity() { return city; }
    public String getNeighborhood() { return neighborhood; }
    public String getStatus() { return status; }
    public String getEmoji() { return emoji; }
    public String getTone() { return tone; }
    public String getCreatedAt() { return createdAt; }

    public byte[] getImage() {
        return image == null ? new byte[0] : image.clone();
    }

    public List<Comment> getComments() {
        return List.copyOf(comments);
    }
}
