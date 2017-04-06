#!groovy
timestamps {
    node {
        catchError {
            checkout scm
			def latticeVersion = "1.0.0.${env.BUILD_NUMBER}"
            // Run bash with -it to keep the container alive while we copy files in and run the build
            docker.image('frekele/ant:1.9.7-jdk8').withRun('-it', 'bash') {c ->
			    sh """
                docker cp ./ ${c.id}:/root
				docker exec ${c.id} ant -f /root/source/build.xml dist
                """
            }
			//aggiungere il task maven install
			/*docker.image('maven:3.3.3-jdk-8').withRun('-it -v /var/lib/m2:/root/.m2', 'bash') {c ->
                sh """
                docker cp ./ ${c.id}:/root
                docker exec ${c.id} mvn -U -f /root/ package
                mkdir -p target
                docker cp ${c.id}:/root/target/imos.jar target/imos.jar
                """
            }*/

        }
        //step([$class: 'Mailer', recipients: '5gex-devel@tmit.bme.hu'])
    }
}
