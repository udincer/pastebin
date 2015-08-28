package pastebin;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * This interface is @autowired into the BinController. That means Spring Data provides the implementation implicitly.
 */
public interface ContentRepository extends MongoRepository<TextContent, String> {
    TextContent findByToken(String token);
}
