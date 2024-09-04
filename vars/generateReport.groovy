#!/usr/bin/groovy

def call(String name = 'human') {
    // Any valid steps can be called from this code, just like in other
    // Scripted Pipeline
    def ci = new com.utils.Integration()
    def text = ci.GetDataForReport(currentBuild.getRawBuild())
    try {
            writeFile file: 'report.html', text: "${text}"
    } catch (Exception e) {
        echo "catch NotSerializableException for write report.html"
    }
    try {
            writeFile file: "Daily_Build_ReleaeNotes_${VERSION}.html", text: "${text}"
    } catch (Exception e) {
        echo "catch NotSerializableException for write Daily_Build_ReleaeNotes_${VERSION}.html"
    }
    try{
        rtp parserName: 'HTML', stableText: ' ${FILE:report.html} '
    } catch (Exception e) {
        echo "catch NotSerializableException for publish reports"
    }

}
