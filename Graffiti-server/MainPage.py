from App import *
class MainPage(BaseHandler):
    def get(self):
        self.write("Graffiti Codes")
        self.finish()
