pipeline {
    agent any

    stages {
        stage('example') {

            steps {
			   
                script {
				  echo "NOW IS ${BUILD_NUMBER}"
				  echo "NOW2 IS ${env.BUILD_NUMBER}"
				}
            }
        }
		}
   
}
