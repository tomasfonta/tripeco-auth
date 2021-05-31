package tripeco.auth.config

import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.convert.MongoConverter
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver
import tripeco.auth.model.User

@Configuration
class MongoConfig(private val mongoTemplate: MongoTemplate, private val mongoConverter: MongoConverter) {

    private val indexedClasses = listOf(User::class.java)

    @EventListener(ApplicationReadyEvent::class)
    fun initIndicesAfterStartup() {
        val resolver = MongoPersistentEntityIndexResolver(mongoConverter.mappingContext)
        indexedClasses.forEach { indexedClass ->
            resolver.resolveIndexFor(indexedClass).forEach {
                mongoTemplate.indexOps(indexedClass).ensureIndex(it)
            }
        }
    }

}
