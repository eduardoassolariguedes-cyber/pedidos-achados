
package br.com.achadoseperdidos.repository;

import br.com.achadoseperdidos.model.LostFoundPost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<LostFoundPost, Long> {
}

    

