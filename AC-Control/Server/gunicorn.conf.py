bind = "0.0.0.0:3001"
keyfile = "./certs/ACServer.key"
certfile = "./certs/ACServer.crt"  
wsgi_app = "Server:appStart()"

def worker_exit(server, worker):
    print("worker_exit")
    server.app.wsgi().cleanup()
    
