from bloatit.htmlrenderer.htmltools import HtmlTools
from bloatit.htmlrenderer.pagecontent.pagecontent import PageContent

class IndexContent(PageContent):

    def get_code():
        return "index"

    
    def __init__(self, session):
        self.session = session


    def get_title(self):
        return "Finance free software"

    def generate_body(self, text):
        text.write("<h2>Welcome in "+HtmlTools.generate_logo()+" website</h2>")
        text.write(HtmlTools.generate_logo()+" is a wonderful website !")
        text.write(100*" is a wonderful website !")