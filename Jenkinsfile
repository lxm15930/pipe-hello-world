pipeline {
    agent any
    stages {
        stage('pmd') {
            steps {
                cmd "mvn pmd:pmd"
            }
        }
    }
}
