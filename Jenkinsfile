#!groovy
timestamps {
    node {
        catchError {
            checkout scm
			def latticeVersion = "1.0.0.${env.BUILD_NUMBER}"
            // Run bash with -it to keep the container alive while we copy files in and run the build
            docker.image('frekele/ant:1.9.7-jdk8').withRun('bash') {c ->
			    sh """
                docker cp ./ ${c.id}:/root
				docker exec ${c.id} ant -f lattice-monitoring-framework/source/build.xml dist
                """
            }
        }
        //step([$class: 'Mailer', recipients: '5gex-devel@tmit.bme.hu'])
    }
}
