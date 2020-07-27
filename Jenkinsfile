pipeline {
    agent any
    
    tools {
        maven 'mvn-3.5.4'
    }
    stages {
        stage('pmd') {
            steps {
                sh "mvn pmd:pmd"
            }
        }
    }
    post {
        always {
            pmd(canRunOnFailed: true, pattern: '**/target/pmd.xml')
        }
    } 
}
