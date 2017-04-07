#!groovy
timestamps {
    node {
        catchError {
            checkout scm
            // Run bash with -it to keep the container alive while we copy files in and run the build
            docker.image('frekele/ant:1.9.7-jdk8').withRun('-it -v /var/lib/m2:/root/.m2', 'bash') {c ->
			    sh """
                docker cp ./ ${c.id}:/root
				docker exec ${c.id} ant -f /root/source/build.xml dist
                mkdir -p jars
                mkdir -p config
                docker cp ${c.id}:/root/jars jars
                cd jars
                mylib=`ls monitoring-bin-controller*.jar`
                echo "mylib: \$mylib"              
                docker cp  \$mylib ${c.id}:/root/.m2/repository/eu/fivegex/monitoring/control/controller/monitoring-bin-controller/0.7.1/
              """
            }
			
			echo "Archiving jars..."
			archive 'jars/*.jar'
			echo "Archiving confs..."
			archive 'conf/*'
			
			echo "Running deployment script..."
			sh "./deploy-lattice-test.sh"
			echo "...done"
        }
        //step([$class: 'Mailer', recipients: '5gex-devel@tmit.bme.hu'])
    }
}