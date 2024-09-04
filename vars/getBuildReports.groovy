#!/usr/bin/groovy

def call(body) {
    def ci = new com.utils.Integration()
    def id = 'Ib11ceddebd5d9b6db8debbcec07d99a5992e1d7c'
    def number = ci.getChangeNumber(id)
    println "------------------------"
    println number
    println "-------------------------"
    def issue = ci.getIssue('15')
    println issue
}
