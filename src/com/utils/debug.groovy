@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7.1')

import groovyx.net.http.HTTPBuilder
import groovyx.net.http.ContentType
import static groovyx.net.http.Method.*
import groovy.json.JsonSlurper 
import groovy.json.JsonOutput



@NonCPS
def getChangesSetsSiceLastSuccessfulBuild(build){
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

def formatChangesForFeishu(changeSets){
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

def formatCommitMesssage(message){
    def topic = message.find("\\W?.*\n\n")
    def module = message.find("[m|M]odule\\s?:\\s?\\w+")
    def project = message.find("[p|P]roject\\s?:\\s?.*")
    def type = message.find("[T|t]ype\\s?:\\s?.*")
    def tracking = message.find("[T|t]racking-[i|I]d\\s?:\\s?.*")
    def data = [:]
    if(module){
        data['module'] = module.split(':')[-1].trim()
    }else{
        data['module'] = 'NA'
    }
    if(project){
        data['project'] = module.split(':')[-1].trim()
    }else{
        data['project'] = 'NA'
    }
    if(type){
        data['type'] = module.split(':')[-1].trim()
    }else{
        data['type'] = 'NA'
    }
    if(tracking){
        data['tracking'] = module.split(':')[-1].trim()
    }else{
        data['tracking'] = 'NA'
    }
    data['topic'] = topic
    return data
}


def job = "MCU/DailyBuild-ADAS-PB101-dev"
def number = 35
def currentBuild = Jenkins.getInstance().getItemByFullName(job).getBuildByNumber(number)
def env = currentBuild.getEnvironment()
//getChanges(currentBuild)
