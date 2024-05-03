bind = "0.0.0.0:3001"

def worker_abort(worker):
    print("worker_abort")

def worker_exit(server, worker):
    print("worker_exit")
    #server.app.wsgi().cleanup()

def on_exit(server):    
    print("Performing Cleanup")
    
