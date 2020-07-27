pipeline {
    agent any

    stages {
        stage('example') {
            steps {
                script {
				 def  name = 'world'
				 print "hello ${name}"
				 print 'hello ${name}'
				 
                 def sayhello(String name) {
				   print "hello ${name}"
				 }
				 sayhello('pipeline')
				 
				}
            }
        }
    }
}
