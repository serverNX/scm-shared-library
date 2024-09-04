#!/usr/bin/groovy

def call(type,REPO,BRANCH)
{
    if(type == 'repo'){
        def xml = env.'JOB_BASE_NAME'.split('-')[1].toLowerCase() + '.xml'
        checkout([$class: 'RepoScm', cleanFirst: true, currentBranch: true,destinationDir: 'source', forceSync: true, manifestBranch: BRANCH, manifestFile: xml, manifestRepositoryUrl: "ssh://jenkins@172.16.5.149:29418/${REPO}.git", quiet: true, resetFirst: true])

    }else(
        checkout([$class: 'GitSCM',branches: [[name: 'pb101-dev']], 
        extensions: [submodule(parentCredentials: true, reference: ''), [$class: 'RelativeTargetDirectory', relativeTargetDir: 'source'], cleanBeforeCheckout()],
        userRemoteConfigs: [[credentialsId: 'jenkins-ssh-227', url: "ssh://172.16.5.149:29418/${REPO}.git"]]])
    )
}
