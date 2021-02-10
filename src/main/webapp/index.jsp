<html>
    <head>
      <!-- Latest compiled and minified CSS -->
      <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">

        <title>Baggage Handling Demo</title>
    </head>
    <body>
      <div class="container">

      <div class="container-fluid">
        <div class="row">
          <div class="col-xs-12">
            <img src="http://www.dataversity.net/wp-content/uploads/2013/04/Oracle_NoSQLDatabase_Logo_650.gif?x23053" width="150" height="85">
          </div>
        </div> <!-- End row -->


        <div class="row">
          <div class="col-xs-6 col-xs-offset-3">
            <div class="panel">
              <p>Please enter your credentials.</p>
            </div>
            <form method="post" action="login">
              <div class="form-group">
                  <input type="text" class="form-control" name="username">
              </div>
              <div class="form-group">
                <input type="password" class="form-control" name="password">
              </div>
              <div style="color:red; margin-bottom:5px;">
                ${message}
              </div>
            <input type="submit" class="btn btn-default" style="border: 1px solid !important;" value="login" />
            </form>
          </div>
        </div>
      </div>
    </body>
</html>
