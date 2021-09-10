def version = '0.0.0'

pipeline {
    agent any

    tools {
        gradle "gradle7.2"
    }

    stages {
//            stage('Get Version'){
//                 steps{
//                     script {
//                         version = sh script: 'gradle version', returnStdout: true
//                         //version = version.substring(url.indexOf(":")+8, url.indexOf(" ", url.indexOf(":")+8))
//                     }
//                     echo "${version}"
//                 }
//             }
            stage('Build') {
                steps {
                    sh "gradle jpi"
                }

                post {
                    success {
//                         zip zipFile: "rjl${version}.zip", archive: true, overwrite: true, glob: 'README.md, build/libs/*.hpi'
                        zip zipFile: "rjl.zip", archive: true, overwrite: true, glob: 'README.md, build/libs/*.hpi'
                        archiveArtifacts 'build/libs/*.hpi'
                    }
                }
            }
            /*stage('Install') {
                steps {
                    sh "gradle installJenkinsServerPlugins"
                }
            }*/
        }
}
