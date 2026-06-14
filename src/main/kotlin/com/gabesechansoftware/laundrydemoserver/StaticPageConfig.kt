package com.gabesechansoftware.laundrydemoserver

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Spring Boot only auto-maps the root path "/" to index.html. Subdirectory
 * "directory" URLs like /adminapp/ are not mapped to their index.html, so we
 * forward them explicitly.
 */
@Configuration
class StaticPageConfig : WebMvcConfigurer {
    override fun addViewControllers(registry: ViewControllerRegistry) {
        registry.addViewController("/adminapp").setViewName("forward:/adminapp/index.html")
        registry.addViewController("/adminapp/").setViewName("forward:/adminapp/index.html")
    }
}
