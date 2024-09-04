package  com.utils

@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7.1')

import groovyx.net.http.HTTPBuilder
import groovyx.net.http.ContentType
import static groovyx.net.http.Method.*

@NonCPS
def PostFeishuMessage(robotId,template_id,template_version_name,data){  
    def http = new HTTPBuilder('https://open.feishu.cn/')
    http.request(POST) {
        uri.path = '/open-apis/bot/v2/hook/' + robotId
        body = [ msg_type: 'interactive',card: [ type: "template",data: [ template_id: template_id,template_version_name: template_version_name,template_variable:data ]]]
        requestContentType = ContentType.JSON
        response.success = { resp ->
            println "Success! ${resp.status}"
            println resp.getData()
        }
        response.failure = { resp ->
            println "Request failed with status ${resp.status}"
        }
        println "--------------------------------"
        println "ggggggggggggggggggggggggggggggg"
        println body
        println '--------------------------------'
    }
}


return  this
