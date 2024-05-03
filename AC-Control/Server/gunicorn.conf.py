bind = "0.0.0.0:3001"

def worker_exit(server, worker):
    print("worker_exit")
    server.app.wsgi().cleanup()
    
