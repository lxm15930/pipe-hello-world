pipeline {
    agent any

    stages {
        stage('pmd') {
            steps {			   
             bat "mvn pmd:pmd"
            }
        }
		}
		
	post {
	 always{
	    pmd(canRunOnFailed: true,pattern:'**/target/pmd.xml')
	 
	 }
	
	}
   
}
