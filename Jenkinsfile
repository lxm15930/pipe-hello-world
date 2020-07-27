pipeline {
    agent any

    stages {
        stage('build') {
            steps {
			    bat "mvn clean"
			    bat "type"
                echo 'Hello World jenkins'
            }
        }
    }
}
