package  com.utils


//@NonCPS
def postBuildInfo(){
    def robotId = 'f160382a-74b7-4514-b4d2-185ee684dc87'
    def template_id = 'AAqHIy7ngeOwq'
    def template_version_name = '1.0.14'
    def data = []
    //def p = 
    PostFeishuMessage(robotId,template_id,template_version_name,data).post()
    echo "--------------------------------"
    echo "oooooooooooooooooooooooooooooo"
    echo '--------------------------------'
}

postBuildInfo()

return this
