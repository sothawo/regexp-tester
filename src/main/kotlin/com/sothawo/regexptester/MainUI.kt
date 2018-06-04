package com.sothawo.regexptester

import com.vaadin.annotations.Push
import com.vaadin.annotations.Theme
import com.vaadin.data.HasValue
import com.vaadin.server.VaadinRequest
import com.vaadin.shared.ui.ContentMode
import com.vaadin.spring.annotation.SpringUI
import com.vaadin.ui.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.regex.Pattern

/**
 * @author P.J. Meisch (pj.meisch@sothawo.com)
 */
@SpringUI
@Push
@Theme("custom")
class MainUI : UI() {

    private var pattern: Pattern? = null
    private var stringValue: String? = null

    private val quotePrefix = "Java: "
    private val quotedPattern = Label(quotePrefix)

    val patternChangeHandler: (HasValue.ValueChangeEvent<String>) -> Unit = { evt ->
        run {
            val newPattern = evt.value
            LOG.info("new pattern: $newPattern")
            regExp.removeStyleNames("ok", "error")
            try {
                pattern = Pattern.compile(newPattern)
                quotedPattern.value = quotePrefix + newPattern.replace("\\", "\\\\")
                regExp.addStyleName("ok")
            } catch (e: Exception) {
                LOG.debug("invalid pattern $newPattern")
                pattern = null
                quotedPattern.value = quotePrefix
                regExp.addStyleName("error")
            }
            checkPattern()
        }
    }
    private var regExp = TextField("regular expression:", patternChangeHandler).apply {
        setWidth("100%")
    }

    private val testString = TextField("string to test:", { evt ->
        run {
            val newValue = evt.value
            LOG.info("new string: $newValue")
            stringValue = newValue
            checkPattern()
        }
    }).apply {
        setWidth("100%")
    }

    private val groupsPrefix = "groups:<br/>"
    private val labelGroups = Label(groupsPrefix, ContentMode.HTML)


    override fun init(request: VaadinRequest?) {
        content = VerticalLayout().apply {
            val panel = Panel().apply {
                content = VerticalLayout().apply {
                    setSizeFull()
                    addComponents(regExp, quotedPattern, testString, labelGroups)
                }
                setWidth("30%")
            }
            addComponent(panel)
            setComponentAlignment(panel, Alignment.MIDDLE_CENTER)
        }
    }


    private fun checkPattern() {
        var newGroups = groupsPrefix
        testString.removeStyleNames("error", "ok")
        pattern?.matcher(stringValue ?: "")?.let {
            LOG.info("checking pattern")
            if (it.matches()) {
                testString.addStyleName("ok")
                for (group in 0..it.groupCount())
                    newGroups += "group $group: ${it.group(group)} <br/>"
            } else
            {
                testString.addStyleName("error")
            }

        }
        labelGroups.value = newGroups
    }

    companion object {
        val LOG: Logger = LoggerFactory.getLogger(MainUI::class.java)
    }
}
