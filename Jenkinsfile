#!groovy
timestamps {
    node {
        try {
            checkout scm
            // Run bash with -it to keep the container alive while we copy files in and run the build
            docker.image('frekele/ant:1.9.7-jdk8').withRun('-it -v /var/lib/m2:/root/.m2', 'bash') {c ->
			    sh """
                docker cp ./ ${c.id}:/root
				docker exec ${c.id} ant -f /root/source/build.xml dist
                mkdir -p dist
                docker cp ${c.id}:/root/jars dist
				docker exec ${c.id} mkdir -p /root/.m2/repository/cc/clayman/logging/1.0.0/
                docker cp  ./libs/misc/restconsole-1.0.0.jar ${c.id}:/root/.m2/repository/cc/clayman/logging/1.0.0/restconsole-1.0.0.jar
              """
            }
			
			echo "Archiving jars..."
			archive 'dist/jars/*.jar'
			echo "Archiving confs..."
			archive 'conf/*'
			
			currentBuild.result = 'SUCCESS'
        } catch (any) {
			currentBuild.result = 'FAILURE'
			throw any
		} finally {
			//step([$class: 'Mailer', recipients: '5gex-devel@tmit.bme.hu'])
		}
    }
}