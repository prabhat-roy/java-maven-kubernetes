def gv_script
pipeline {
    agent { label 'Jenkins-Agent' }
    environment {
        NEXUS_IP = "10.0.1.6"	
        K8S_MASTER_IP ="10.0.1.3"        
        nexus_cred = "nexus"
	NEXUS_IMAGE_URL = "${NEXUS_IP}:8082"
        IMAGE_NAME = "docker-images" 
        DOCKERHUB_NAME = "prabhatrkumaroy"
		GITHUB_URL = "https://github.com/prabhat-roy/java-tomcat.git"
    }
    tools {
        jdk 'Java'
        maven 'Maven'
    }
    stages {
        stage("Init") {
            steps {
                script {
                    gv_script = load"script.groovy"
                }
            }
        }
        stage("Cleanup Workspace") {
            steps {
                script {
                    gv_script.cleanup()
                }
            }
        }
        stage("Checkout from Git Repo") {
            steps {
                script {
                    gv_script.checkout()
                }
            }
        }
        stage("OWASP FS Scan") {
            steps {
                script {
                    gv_script.owasp()
                }
            }
        }
        stage("SonarQube Analysis") {
            steps {
                script {
                    gv_script.sonaranalysis()
                }
            }
        }
        stage("Trivy FS Scan") {
            steps {
                script {
                    gv_script.trivyfs()
                }
            }
        }
        stage("Code Compile") {
            steps {
                script {
                    gv_script.codecompile()
                }
            }
        }
        stage("Building Application") {
            steps {
                script {
                    gv_script.buildapplication()
                }
            }
        }
        stage("Docker Build") {
            steps {
                script {
                    gv_script.dockerbuild()
                }
            }
        }
        stage("Trivy Image Scan") {
            steps {
                script {
                    gv_script.trivyimage()
                }
            }
        }
        stage("Docker Scout Image Scan") {
            steps {
                script {
                    gv_script.dockerscout()
                }
            }
        }
        stage("Grype Image Scan") {
            steps {
                script {
                    gv_script.grype()
                }
            }
        }
        stage("Syft Image Scan") {
            steps {
                script {
                    gv_script.syft()
                }
            }
        }
        stage("Docker Push Docker Hub") {
            steps {
                script {
                    gv_script.dockerhub()
                }
            }
        }
        stage("Docker Push Nexus") {
            steps {
                script {
                    gv_script.dockernexus()
                }
            }
        }
       stage("Deployment to K8s") {
            steps {
                script {
                    gv_script.kubernetes()
                }
            }
        }
        stage("Container Removal") {
            steps {
                script {
                    gv_script.removedocker()
                }
            }
        }
    }
    post {
        always {
            sh "docker logout"
            deleteDir()
        }
    }
}

