def cleanup() {
        cleanWs()
}

def checkout() {
        git branch: 'main', credentialsId: 'github', url: "$GITHUB_URL"
}

def owasp() {
    dependencyCheck additionalArguments: '--scan ./ --disableYarnAudit --disableNodeAudit', odcInstallation: 'DP-check'
    dependencyCheckPublisher pattern: '**/dependency-check-report.xml'
}

def sonaranalysis() {
        withSonarQubeEnv(installationName: 'SonarQube', credentialsId: 'sonar') {
            sh "mvn sonar:sonar"
    }
}

def qualitygate() {
        waitForQualityGate abortPipeline: false, credentialsId: 'sonar'
}

def trivyfs() {
        sh "trivy fs ."
}

def codecompile() {
        sh '''
                mvn -f productcatalogue/ clean compile
                mvn -f shopfront/ clean compile
                mvn -f stockmanager/ clean compile
        '''
}

def buildapplication() {
    sh '''
                mvn -f productcatalogue/ clean install -DskipTests
                mvn -f shopfront/ clean install -DskipTests
                mvn -f stockmanager/ clean install -DskipTests
        '''
}

def dockerbuild() {
        sh '''
                docker build . -t ${PRODUCT_IMAGE_NAME}:${BUILD_NUMBER} -f productcatalogue/Dockerfile
                
        '''
 
}

def trivyimage() {
        sh '''
                trivy image ${PRODUCT_IMAGE_NAME}:${BUILD_NUMBER}
                
        '''
        
}

def grype() {
        sh "grype ${IMAGE_NAME}:${BUILD_NUMBER}"       
}

def syft() {
        sh "syft ${IMAGE_NAME}:${BUILD_NUMBER}"       
}

def dockerrun() {
    sh '''
        docker run -dt ${IMAGE_NAME}:${BUILD_NUMBER}"
        docker stop ${IMAGE_NAME}:${BUILD_NUMBER}"
        '''
}

def dockerhub() {
        withCredentials([usernamePassword(credentialsId: 'dockerhub', passwordVariable: 'dockerHubPassword', usernameVariable: 'dockerHubUser')]) {
               sh "docker image tag ${IMAGE_NAME}:${BUILD_NUMBER} ${DOCKERHUB_NAME}/${IMAGE_NAME}:${BUILD_NUMBER}"
               sh "docker login -u ${env.dockerHubUser} -p ${env.dockerHubPassword}"
               sh "docker push ${DOCKERHUB_NAME}/${IMAGE_NAME}:${BUILD_NUMBER}"
               sh "docker pull ${DOCKERHUB_NAME}/${IMAGE_NAME}:${BUILD_NUMBER}"
               sh "docker rmi -f ${DOCKERHUB_NAME}/${IMAGE_NAME}:${BUILD_NUMBER}"
    }
}

def dockernexus() {
        withCredentials([usernamePassword(credentialsId: 'nexus', passwordVariable: 'nexusPassword', usernameVariable: 'nexusUser')]) {
                sh "docker image tag ${IMAGE_NAME}:${BUILD_NUMBER} ${NEXUS_IMAGE_URL}/${IMAGE_NAME}:${BUILD_NUMBER}"
	        sh "docker login -u ${env.nexusUser} -p ${env.nexusPassword} ${NEXUS_IMAGE_URL}"
                sh "docker push ${NEXUS_IMAGE_URL}/${IMAGE_NAME}:${BUILD_NUMBER}"
                sh "docker pull ${NEXUS_IMAGE_URL}/${IMAGE_NAME}:${BUILD_NUMBER}"
                sh "docker rmi -f ${NEXUS_IMAGE_URL}/${IMAGE_NAME}:${BUILD_NUMBER}"
        }
}

def dockerscout() {
        withCredentials([usernamePassword(credentialsId: 'dockerhub', passwordVariable: 'dockerHubPassword', usernameVariable: 'dockerHubUser')]) {
                sh "docker login -u ${env.dockerHubUser} -p ${env.dockerHubPassword}"
                sh "docker scout quickview ${IMAGE_NAME}:${BUILD_NUMBER}"
                sh "docker scout cves ${IMAGE_NAME}:${BUILD_NUMBER}"
                sh "docker scout recommendations ${IMAGE_NAME}:${BUILD_NUMBER}"
        }
}

def kubernetes() {
                 sshagent(['k8s']) {
                        sh "scp -o StrictHostKeyChecking=no service.yml nexus-deployment.yml dockerhub-deployment.yml root@'${K8S_MASTER_IP}':/root"
                        sh "ssh root@'${K8S_MASTER_IP}' kubectl apply -f ."
                        sh "ssh root@'${K8S_MASTER_IP}' rm -rf *.yml"
        }
}

def removedocker() {
                sh "docker rmi -f ${IMAGE_NAME}:${BUILD_NUMBER}"
                sh "docker rmi -f owasp/zap2docker-stable"
                sh "docker system prune --force --all"
                sh "docker system prune --force --all --volumes"
}


return this