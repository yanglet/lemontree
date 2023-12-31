package com.lemontree.global.configuration

import org.springframework.data.domain.*
import org.springframework.stereotype.*
import java.util.*

@Component
class CustomAuditorAware : AuditorAware<String> {
    override fun getCurrentAuditor(): Optional<String> = Optional.of("lemontree-api")
}