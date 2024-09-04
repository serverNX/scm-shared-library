#!/usr/bin/groovy

def call(String name = 'human') {
    // Any valid steps can be called from this code, just like in other
    // Scripted Pipeline
    echo "Hello, ${name}."
    def p = new com.utils.Log().info("郭慧鑫")
    def robotId = 'f160382a-74b7-4514-b4d2-185ee684dc87'
    def template_id = 'AAqHIy7ngeOwq'
    def template_version_name = '1.0.21'
    def artifactory_url = 'http://172.16.5.176:8081/Auto-Software-Department/DailyBuild/' + env.PRODUCT +"/" + env.SYSTEM + "/"+ currentBuild.getDescription().replace('#','%23')
    def ci = new com.utils.Integration()
    def changes = ci.getChanges(currentBuild)
    def nochanges = 'No Changes'
    if(changes.size() > 0){
        nochanges = ''
    }
    def data = [BUILD_URL: env.BUILD_URL,
                BUILD_RESULT: currentBuild.getResult(),
                BUILD_NUMBER: env.BUILD_DISPLAY_NAME,
                JOB_BASE_NAME: env.JOB_BASE_NAME,
                VERSION: env.VERSION,
                BRANCH: env.BRANCH,
                noChanges: nochanges,
                causes:currentBuild.getBuildCauses().get(0).shortDescription,
                PROJECT:env.PRODUCT,
                timestampString: new Date(currentBuild.getStartTimeInMillis()).format("yy-MM-dd.HH:mm", TimeZone.getTimeZone('UTC')),
                durationString: currentBuild.getDurationString(),
                ARTIFACTORY_URL: artifactory_url,
                object_list_1: changes]
    ci.PostFeishuMessage(robotId,template_id,template_version_name,data)
}
