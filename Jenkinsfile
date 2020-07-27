pipeline {
    agent any
    stages {
        stage('pmd') {
            steps {
                bat "mvn pmd:pmd"
            }
        }
    }
}
