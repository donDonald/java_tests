<?php
    //echo phpinfo();    
    //error_reporting(E_ALL);
    error_reporting(E_ERROR | E_WARNING | E_PARSE);
    ini_set('display_errors', 'on');

    $method = new PushSoundMethod;
    $method->handle( $_POST );
?>

<?php
class PushSoundMethod
{

    public function handle( $request ) {
        try {
            $this->doHandle( $request );
        } catch ( Exception $e ) {
            echo 'Exception occured: ',  $e->getMessage(), "\n";
        }
    }

    private function doHandle( $request ) {
        $this->storeData( $request );
        if( $this->getExpectedResult() == TRUE ) {
            echo '{ "result" : "success", "value" : "20.000", "threshold" : "10.000", "text" : "fine"  }';
        } else {
            echo '{ "result" : "success", "value" : "0.000", "threshold" : "10.000", "text" : "fine"  }';
        }
    }

    private function storeData( $request ) {
        $headers = getallheaders();

        $length = $headers['settings-length'];
        if( !$length ) { $length = 'unknown'; }
        $frequency = $headers['settings-frequency'];
        if( !$frequency) { $frequency = 'unknown'; }

        date_default_timezone_set('Europe/Moscow');
        list($usec, $sec) = explode(" ", microtime());
        $date = date('m-d-Y-h-i-s-a', time());
        $filePath = '/var/www/html/audioshots/';
        $tmpFilePath = '/tmp/';
        $fileName = $date . '-' . $usec . '.' . $length . '.' . $frequency . '.wav.zip';
        $rawData = file_get_contents('php://input');
        $handle = fopen( $tmpFilePath . $fileName, 'wb' );
        fwrite ( $handle, $rawData );
        fclose( $handle );

        exec( '7z e -o' . $filePath . ' ' . $tmpFilePath . $fileName );
    }

    function getExpectedResult() {
        return file_exists( "./settings/expectedresultyes" );
    }   

}
?>

