package ru.netology.repository;

import org.springframework.stereotype.Repository;
import ru.netology.model.Post;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class PostRepository {
    private final List<Post> posts = new CopyOnWriteArrayList<>();
    private final AtomicLong currentId = new AtomicLong(1);

    public List<Post> all() {
        return List.copyOf(posts);
    }

    public Optional<Post> getById(long id) {
        return posts.stream().filter(p -> p.getId() == id).findFirst();
    }

    public Post save(Post post) {
        // Синхронизируем блок только для генерации ID и поиска/замены
        synchronized (this) {
            if (post.getId() == 0) {
                // Создание нового поста
                long newId = currentId.getAndIncrement();
                Post newPost = new Post(newId, post.getContent());
                posts.add(newPost);
                return newPost;
            } else {
                // Обновление существующего
                Optional<Post> existing = getById(post.getId());
                if (existing.isPresent()) {
                    // Находим индекс и заменяем (CopyOnWriteArrayList поддерживает set)
                    int index = posts.indexOf(existing.get());
                    Post updatedPost = new Post(post.getId(), post.getContent());
                    posts.set(index, updatedPost);
                    return updatedPost;
                } else {
                    // Стратегия: если не найден — создаем новый (альтернатива: throw exception)
                    return save(new Post(0, post.getContent()));
                }
            }
        }
    }

    public void removeById(long id) {
        posts.removeIf(p -> p.getId() == id);
    }
}
