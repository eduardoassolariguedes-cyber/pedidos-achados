
package br.com.achadoseperdidos.model;

import jakarta.persistence.*;

@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String author;

    @Column(length = 180)
    private String text;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private LostFoundPost post;

    public Comment() {
    }

    public Comment(String author, String text) {
        this.author = author;
        this.text = text;
    }

    public String getAuthor() {
        return author;
    }

    public String getText() {
        return text;
    }

    public void setPost(LostFoundPost post) {
        this.post = post;
    }

    public LostFoundPost getPost() {
        return post;
    }
}
