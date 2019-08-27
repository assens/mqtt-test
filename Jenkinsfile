pipeline {
  agent {
    docker {
      image 'maven:3.6.1-jdk-11-slim'
      args '-v $HOME/.m2:/root/.m2:z -u root'
      reuseNode true
    }
  }
  stages {
    stage('verify') {
      steps {
       sh 'mvn verify'
      }
    }
  }

  post {
    always {
      jacoco( 
        execPattern: '**/*.exec',
        classPattern: '**/classes',
        sourcePattern: '**/src'
      )
    }
  }
}
