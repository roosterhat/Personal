bind = "0.0.0.0:3001"
keyfile = "./certs/ACServer.key"
certfile = "./certs/ACServer.crt"  
wsgi_app = "Server:appStart()"

def worker_abort(worker):
    print("worker_abort")
    print(worker.app.wsgi().test)
    worker.app.wsgi().cleanup()
    
