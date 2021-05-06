### HTML to PDF ###

https://github.com/aashipov/htmltopdf twin, implemented in JavaSE

Expects chromium executable at /usr/bin/chromium. Use chromium.executable system property to override

##### Docker, single instance #####

```docker pull aashipov/htmltopdf-javase:pure && docker run -d --rm --name=htmltopdf-javase -p 8080:8080 aashipov/htmltopdf-javase:pure```

If you’re running Docker on Linux use tmpfs to store incoming files and result.pdf:

```docker pull aashipov/htmltopdf-javase:pure && docker run -d --cap-drop=ALL --name=htmltopdf -p 8080:8080 --tmpfs=/dummy/tmp aashipov/htmltopdf-javase:pure```

OR

Local build & run ```bash build-and-run.bash```
