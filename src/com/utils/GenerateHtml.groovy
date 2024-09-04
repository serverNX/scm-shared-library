//@Grab(group='org.gperfutils', module='gbench', version='0.4.2-groovy-2.1')
import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

firstname = 'guo'
lastname = "huixin"
city = 'beijing'

def writer = new StringWriter()  
    def builder = new groovy.xml.MarkupBuilder(writer)
    builder.html {
        head {
            title"Report"
        }
        body {
            h1"XML encoding with Groovy"
            p"this format can be used as an alternative markup to XML"

            // an element with attributes and text content /
            a(href:'http://groovy.codehaus.org', "Groovy")

            // mixed content /
            p() {
                "This is some"
                "mixed"
                "text. For more see the"
                a(href:'http://groovy.codehaus.org', "Groovy")
                "project"
            }
            table{
                 tr {
                th("firstname")
                th("lastname")
                th("city")
            }
            tr{
                td("$firstname")
                td("$lastname")
                td("$city")
            }
            }
        }
    }

new File('report.html') << writer.toString()

println writer
