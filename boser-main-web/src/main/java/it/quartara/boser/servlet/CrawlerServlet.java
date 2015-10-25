package it.quartara.boser.servlet;

import javax.servlet.annotation.WebServlet;

/**
 * Definition of the two JMS destinations used by the quickstart
 * (one queue and one topic).
@JMSDestinationDefinitions(
    value = {
        @JMSDestinationDefinition(
            name = "java:/queue/HELLOWORLDMDBQueue",
            interfaceName = "javax.jms.Queue",
            destinationName = "HelloWorldMDBQueue"
        ),
        @JMSDestinationDefinition(
            name = "java:/topic/HELLOWORLDMDBTopic",
            interfaceName = "javax.jms.Topic",
            destinationName = "HelloWorldMDBTopic"
        )
    })
 */
@WebServlet("/newSearch")
public class CrawlerServlet {

}
