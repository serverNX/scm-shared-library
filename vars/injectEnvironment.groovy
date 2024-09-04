#!/usr/bin/groovy

def call(body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()
    println '---------------------------------------'
    println config
    println '---------------------------------------'
    def job = env.'JOB_BASE_NAME'
    def scms = currentBuild.rawBuild.getSCMs()
    env.'PRODUCT' = job.split('-')[2]
    env.'SYSTEM' = job.split('-')[1]
    if ( !config.BRANCH && scms.size() == 1 ) {
        env.'BRANCH' = job.split('-')[3]
    }  
    def version = 'vehicle-auto-soc'
    if(config.version){
        version = config.version
    }
    env.'VERSION' = version
    if(scms.size() > 1){
        println("The scm type is ${scms[1]}")
        if( scms[1] instanceof hudson.plugins.repo.RepoScm ){
            env.'REPO_URL' = scms[1].getManifestRepositoryUrl()
            env.'BRANCH' = scms[1].getManifestBranch()
        }else if(  scms[1] instanceof hudson.plugins.git.GitSCM){
            env.'REPO_URL' = scms[1].getRepositories()[0].getURIs()[0]
            env.'BRANCH' = scms[1].getBranches()[0]
        }
    }
    currentBuild.setDescription(env.'VERSION')
 }
