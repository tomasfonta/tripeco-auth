package tripeco.auth.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.thymeleaf.TemplateEngine
import org.thymeleaf.spring5.SpringTemplateEngine
import org.thymeleaf.templatemode.TemplateMode
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver

@Configuration
class EmailConfig {

    @Bean
    fun emailTemplateEngine(): TemplateEngine {
        val templateEngine = SpringTemplateEngine()
        val templateResolver = ClassLoaderTemplateResolver()
        templateResolver.templateMode = TemplateMode.HTML
        templateResolver.characterEncoding = "UTF8"
        templateResolver.checkExistence = true
        templateEngine.addTemplateResolver(templateResolver)
        return templateEngine
    }

}