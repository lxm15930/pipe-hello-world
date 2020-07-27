pipeline {
    agent any

    stages {
        stage('example') {
            steps {
			   
                script {
				  writeFile(file:"base64File",text:"afsererererr323s",encoding:"Base64")
                  def content = readFile(file:"base64File",encoding:"UTF-8")
				  echo "${content}"
				}
            }
        }
    }
}
