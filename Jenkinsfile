def gv_script
pipeline {
    agent { label 'Jenkins-Agent' }
    environment {
        NEXUS_IP = "10.0.1.7"	
        K8S_MASTER_IP ="10.0.1.2"        
        nexus_cred = "nexus"
	    NEXUS_IMAGE_URL = "${NEXUS_IP}:8082"
        PRODUCT_IMAGE_NAME = "product-catalogue"
        SHOPFRONT_IMAGE_NAME = "shopfront"
        STOCKMANAGER_IMAGE_NAME = "stockmanager"
        DOCKERHUB_NAME = "prabhatrkumaroy"
		GITHUB_URL = "https://github.com/prabhat-roy/java-maven-kubernetes.git"
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

    }
    post {
        always {
            sh "docker logout"
            deleteDir()
        }
    }
}
