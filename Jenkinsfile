pipeline {
    agent any
    stages {
        stage('pmd') {
            steps {
                sh "mvn pmd:pmd"
            }
        }
    }
}
