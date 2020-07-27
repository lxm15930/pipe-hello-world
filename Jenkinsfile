pipeline {
    agent any

    stages {
        stage('example') {
            steps {
                script {
				 def hello(String name) {
				   print "hello ${name}"
				 }
                 
				 hello("pipeline")
				 
				}
            }
        }
    }
}
