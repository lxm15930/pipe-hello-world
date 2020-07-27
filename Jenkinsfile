pipeline {
    agent any

    stages {
        stage('build') {
            steps {
			    bat "mvn clean package"
                echo 'Hello World jenkins'
            }
        }
    }
}
