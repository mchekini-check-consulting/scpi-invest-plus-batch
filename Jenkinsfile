node("ci-node") {
    def GIT_COMMIT_HASH = ""

    stage("Checkout") {
        checkout scm
        GIT_COMMIT_HASH = sh(script: "git log -n 1 --pretty=format:'%H'", returnStdout: true).trim()
    }

    stage("Build Jar file") {
        sh "chmod +x mvnw && ./mvnw clean package -DskipTests"
    }

    stage("Build Docker Image") {
        sh "docker build -t mchekini/scpi-invest-plus-batch:${GIT_COMMIT_HASH} ."
    }

    stage("Push Docker Image") {
        withCredentials([usernamePassword(credentialsId: 'mchekini', passwordVariable: 'password', usernameVariable: 'username')]) {
            sh "docker login -u $username -p $password"
            sh "docker push mchekini/scpi-invest-plus-batch:${GIT_COMMIT_HASH}"
            sh "docker rmi mchekini/scpi-invest-plus-batch:${GIT_COMMIT_HASH}"
        }
    }

    stage("Update values.yaml for Helm") {
        sh "sed -i 's/tag: \".*\"/tag: \"${GIT_COMMIT_HASH}\"/' helm/values.yaml"
    }

    stage("Commit & Push updated values.yaml") {
        withCredentials([usernamePassword(credentialsId: 'github-creds-id', usernameVariable: 'GIT_USERNAME', passwordVariable: 'GIT_PASSWORD')]) {
            sh """
                git config user.email "jenkins@ci.com"
                git config user.name "jenkins"
                git add helm/values.yaml
                git commit -m "chore: update batch image tag to ${GIT_COMMIT_HASH}" || echo 'No changes to commit'
                git push https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com/mchekini-check-consulting/scpi-invest-plus-batch.git HEAD:main
            """
        }
    }
}
