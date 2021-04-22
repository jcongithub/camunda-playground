1. Generate the project from https://start.camunda.com/
2. Import it into Eclipse
3. Run Application.java
4. http://localhost:8080 login with demo/demo
5. Click Cockpit, you should see one process
6. Download Camunda Modeler https://camunda.com/download/modeler/
7. Open src/main/rersources/process.bpmn with Camunda Modeler


Branches
1. feature/basic
2. feature/user-interaction-task





Test
curl -i -X POST http://localhost:8080/order -d "customer=John&quantity=2&note=Veg"



ToDo
1. Run it using mvn coomand
2. Camunda thread model?
3. Test shutdown application before all processes are finished, check if unfinished processes are resumed after restart the application
4. Add a scheduled task to repeatedly generate orders
5. Add a task to a user approval before orders can be continually processed
