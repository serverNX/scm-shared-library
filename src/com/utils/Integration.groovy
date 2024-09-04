package  com.utils

@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7.1')
@Grab('com.urswolfer.gerrit.client.rest:gerrit-rest-java-client:0.8.8')


import groovyx.net.http.HTTPBuilder
import groovyx.net.http.ContentType
import static groovyx.net.http.Method.*
import groovy.json.JsonSlurper 
import groovy.json.JsonOutput
import com.google.gerrit.extensions.api.GerritApi
import com.google.gerrit.extensions.common.ChangeInfo
import com.urswolfer.gerrit.client.rest.GerritAuthData
import com.urswolfer.gerrit.client.rest.GerritRestApiFactory
import com.google.gson.JsonElement;


@NonCPS
def GetChangesSetsSiceLastSuccessfulLBuild(build){
    def jsonSlurper = new JsonSlurper()
    def changes = []
    def changeSets = new java.util.ArrayList()
    def sumBuild = build
    while(sumBuild.getPreviousBuild() && sumBuild.getNumber() > build.getPreviousSuccessfulBuild().getNumber() ){
        if(sumBuild.changeSets.size()>0){
            changeSets.addAll(sumBuild.changeSets)
        }
        sumBuild = sumBuild.getPreviousBuild()
    }
    return changeSets
}

@NonCPS
def FormatChangesForFeishu(changeSets){
    def jsonSlurper = new JsonSlurper()
    def changes = []
    changeSets.each{ 
      cs_list -> cs_list.each() { 
        cs -> hadChanges = true 
        change = '{ "author":"' + cs.author + '","mail": "'+cs.getAuthorEmail()  + '" ,"commitId": "' + cs.revision + '" ,"message": "' + "${cs.getComment().replaceAll('"','') }"+ '"}'
        changes.add(jsonSlurper.parseText(change))
      }
    }
    return changes
}

@NonCPS
def FormatCommitMesssage(message){
    def topic = message.findAll(".*\n")
    def module = message.find("[m|M]odule\\s?:\\s?\\w+")
    def project = message.find("[p|P]roject\\s?:\\s?.*")
    def type = message.find("[T|t]ype\\s?:\\s?.*")
    def tracking = message.find("[T|t]racking-[i|I]d\\s?:\\s?.*")
    def changeId = message.find("Change-Id\\s?:\\s?.*")
    def data = [:]
    if(topic){
        data['topic'] =topic[0].trim()
    }else{
        data['topic'] = message
    }
    if(module){
        data['module'] = module.split(':')[-1].trim()
    }else{
        data['module'] = 'NA'
    }
    if(project){
        data['project'] = project.split(':')[-1].trim()
    }else{
        data['project'] = 'NA'
    }
    if(type){
        data['type'] = type.split(':')[-1].trim()
    }else{
        data['type'] = 'NA'
    }
    if(tracking){
        data['tracking'] = tracking.split(':')[-1].trim()
    }else{
        data['tracking'] = 'NA'
    }
    if(changeId){
        data['changeId'] = changeId.split(':')[-1].trim()
    }else{
        data['changeId'] = 'NA'
    }
    return data
}

@NonCPS
def PostFeishuMessage(robotId,template_id,template_version_name,data){  
    def http = new HTTPBuilder('https://open.feishu.cn/')
    http.request(POST) {
        uri.path = '/open-apis/bot/v2/hook/' + robotId
        body = [ msg_type: 'interactive',card: [ type: "template",data: [ template_id: template_id,template_version_name: template_version_name,template_variable:data ]]]
        requestContentType = ContentType.JSON
        response.success = { resp ->
            println "Success! ${resp.status}"
        }
        response.failure = { resp ->
            println "Request failed with status ${resp.status}"
        }
        println "------------- Finish Post ------------------"
    }
}

@NonCPS
def GetChangeNumber(id){
    GerritRestApiFactory gerritRestApiFactory = new GerritRestApiFactory()
    GerritAuthData.Basic authData = new GerritAuthData.Basic('http://172.16.5.149/', 'jenkins', 'nzztMReKEK46bOJ6IpVdsrQYPV+RsZyO/D+uphalag')
    GerritApi gerritApi = gerritRestApiFactory.create(authData)
    println("Gerrit String Query is : change:${id}")
    List<ChangeInfo> changes = gerritApi.changes().query("change:${id}").withLimit(10).get();
    def number = 0
    if(changes.size() > 0 ){
      number = changes[-1]._number
    }
    return number
}

@NonCPS
def GetIssue(id){
    def http = new HTTPBuilder('http://172.16.5.149:3000')
    http.auth.basic('guohx','Qwer1234')
    def issue 
    http.request(GET) {
       uri.path = '/issues/' + id + '.json'
       requestContentType = ContentType.JSON
       response.success = { resp ,reader ->
         issue =  reader
         println "Success! ${resp.status}"
       }
       response.failure = { resp  ->
         println "Request failed with status ${resp.status}"
       }
    }
    return issue
}

@NonCPS
def GetDataForReport(build){
    def writer = new StringWriter()  
    def builder = new groovy.xml.MarkupBuilder(writer)
    def cause = build.getCauses().get(0).shortDescription
    def duration = build.getDurationString()
    def changeSets = GetChangesSetsSiceLastSuccessfulLBuild(build)
    def message = [:]
    def BugUrl = "http://172.16.5.149:3000/issues/"
    def ChangeUrl = "https://172.16.5.149/"
    def date = new Date(build.getStartTimeInMillis()).format("yy-MM-dd.HH:mm", TimeZone.getTimeZone('UTC'))
    def atrifactary = "\\\\172.16.5.176\\Auto-Software-Department\\DailyBuild\\" + env.PRODUCT + "\\" + env.SYSTEM + "\\" + env.VERSION
    builder.with {
        html {
            head {
            title"Daily Build ${env.JOB_BASE_NAME} Summary"
              style(type:"text/css", '''
                body {
                  background: #fafafa url(https://jackrugile.com/images/misc/noise-diagonal.png);
                  color: #444;
                  font: 100%/30px 'Helvetica Neue', helvetica, arial, sans-serif;
                  text-shadow: 0 1px 0 #fff;
                }

                strong {
                  font-weight: bold; 
                }

                em {
                  font-style: italic; 
                }

                table {
                  background: #f5f5f5;
                  border-collapse: separate;
                  box-shadow: inset 0 1px 0 #fff;
                  font-size: 12px;
                  line-height: 24px;
                  margin: 10px;
                  text-align: left;
                  width: 900px;
                } 

                th {
                  background: url(https://jackrugile.com/images/misc/noise-diagonal.png), linear-gradient(#777, #444);
                  border-left: 1px solid #555;
                  border-right: 1px solid #777;
                  border-top: 1px solid #555;
                  border-bottom: 1px solid #333;
                  box-shadow: inset 0 1px 0 #999;
                  color: #fff;
                  font-weight: bold;
                  padding: 10px 15px;
                  position: relative;
                  text-shadow: 0 1px 0 #000;  
                }

                th:after {
                  background: linear-gradient(rgba(255,255,255,0), rgba(255,255,255,.08));
                  content: '';
                  display: block;
                  height: 25%;
                  left: 0;
                  margin: 1px 0 0 0;
                  position: absolute;
                  top: 25%;
                  width: 100%;
                }

                th:first-child {
                  border-left: 1px solid #777;  
                  box-shadow: inset 1px 1px 0 #999;
                }

                th:last-child {
                  box-shadow: inset -1px 1px 0 #999;
                }

                td {
                  border-right: 1px solid #fff;
                  border-left: 1px solid #e8e8e8;
                  border-top: 1px solid #fff;
                  border-bottom: 1px solid #e8e8e8;
                  padding: 10px 15px;
                  position: relative;
                  transition: all 300ms;
                }

                td:first-child {
                  box-shadow: inset 1px 0 0 #fff;
                } 

                td:last-child {
                  border-right: 1px solid #e8e8e8;
                  box-shadow: inset -1px 0 0 #fff;
                } 

                tr {
                  background: url(https://jackrugile.com/images/misc/noise-diagonal.png); 
                }

                tr:nth-child(odd) td {
                  background: #f1f1f1 url(https://jackrugile.com/images/misc/noise-diagonal.png); 
                }

                tr:last-of-type td {
                  box-shadow: inset 0 -1px 0 #fff; 
                }

                tr:last-of-type td:first-child {
                  box-shadow: inset 1px -1px 0 #fff;
                } 

                tr:last-of-type td:last-child {
                  box-shadow: inset -1px -1px 0 #fff;
                } 

                tbody:hover td {
                  color: transparent;
                  text-shadow: 0 0 3px #aaa;
                }

                tbody:hover tr:hover td {
                  color: #444;
                  text-shadow: 0 1px 0 #fff;
                }
              ''')
            }
            body {
                h1"Build Info ${env.JOB_BASE_NAME} Summary "
                p() {
                  table{
                     tr{
                        td("Result : ")
                        td(build.result)
                      }
                      tr{
                        td("Build : ")
                        td{
                          	a(href:"${env.BUILD_URL}" ,"${env.BUILD_URL}")
                          }
                      }
                      tr{
                        td("Project : ")
                        td(env.PRODUCT)
                      }
                      tr{
                        td("Date : ")
                        td(date)
                      }
                      tr{
                        td("Duration : ")
                        td(duration)
                      }
                      tr{
                         td("Cause : ")
                         td(cause)
                      }
                      tr{
                        td("Branch : ")
                        td(env.BRANCH)
                      }
                      tr{
                        td("Tag : ")
                        td(env.VERSION)
                      }
                      if("${build.result}" == 'SUCCESS'){
                        tr{
                          td("Atrifactary : ")
                          td{ 
                            a(href:"${atrifactary}" ,atrifactary)
                          }
                        }
                      }
                  }
                  hr( class:"dashed",width: "900px", align:"left")
                  h1"Build Changes"
                  table{
                      tr {
                        th("Change")
                        th("Subject")
                        th("Owner")
                        th("Type")
                        th("Module")
                        th("Tracking-id")
                      }
                      if(changeSets.size() ==0 ){
                        td(colspan:6 ,"No Changes")
                      }else{
                        changeSets.each{ 
                            cs_list -> cs_list.each() {cs ->
                                tr{
                                    if( cs instanceof hudson.plugins.repo.ChangeLogEntry ){
                                      message = FormatCommitMesssage(cs.getMsg())
                                    }else{
                                      message = FormatCommitMesssage(cs.getComment())
                                    }
                                    if(message.tracking != 'NA'){
                                      BugUrl = "http://172.16.5.149:3000/issues/${message.tracking}"
                                    }
                                    Change = GetChangeNumber(message.changeId)
                                    ChangeUrl = "https://172.16.5.149/${Change}"
                                    td{
                                      a(href:"${ChangeUrl}" ,Change)
                                    }
                                    td(message.topic)
                                    td(cs.getAuthorName())
                                    td(message.type)
                                    td(message.module)
                                    td{
                                      a(href:"${BugUrl}" ,message.tracking)
                                    }
                                }
                            }
                        }
                      }
                  }
                  if("${build.result}" != 'SUCCESS'){
                    h1"CONSOLE OUTPUT"
                    build.getLog(100).each() { line ->
                      p(line)
                    }
                  }
               }
            }
        }
    }
    new File("${build.getRootDir()}/report.html") << writer.toString()
    return writer.toString()
}


return this
