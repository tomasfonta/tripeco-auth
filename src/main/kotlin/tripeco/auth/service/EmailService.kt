package tripeco.auth.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import tripeco.auth.model.User
import java.nio.charset.StandardCharsets
import javax.mail.internet.MimeMessage

private const val TEST_EMAILS_DOMAIN = "fontanarrosatomas@gmail.com"

@Service
class EmailService(private val mailSender: JavaMailSender,
                   private val templateEngine: TemplateEngine,
                   @Value("\${spring.mail.username}") private val from: String,
                   @Value("\${production}") private val production: Boolean,
                   @Value("\${tripeco.notification.header}") private val header: String) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Async
    fun newUser(user: User, password: String) {
        val to = user.email
        if (production || to.contains(TEST_EMAILS_DOMAIN)) {
            val ctx = Context()
            ctx.setVariable("user", user)
            ctx.setVariable("password", password)
            val mimeMessage: MimeMessage = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(mimeMessage, StandardCharsets.UTF_8.name())
            val htmlContent = this.templateEngine.process("newUser.html", ctx)
            helper.setFrom(from)
            helper.setTo(user.email)
            helper.setSubject("Bienvenido to Tripeco!")
            helper.setText(htmlContent, true)
            mailSender.send(mimeMessage)
        } else {
            logger.warn("Email to $to was not sent because it is not a valid test email")
        }
    }

    @Async
    fun resendPasswordAccessUser(user: User, password: String) {
        val ctx = Context()
        ctx.setVariable("password", password)
        ctx.setVariable("header", header)
        val mimeMessage: MimeMessage = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(mimeMessage, StandardCharsets.UTF_8.name())
        val htmlContent = this.templateEngine.process("resendPasswordUser.html", ctx)
        helper.setFrom(from)
        helper.setTo(user.email)
        helper.setSubject("Tripeco Password resend")
        helper.setText(htmlContent, true)
        mailSender.send(mimeMessage)
    }

}
