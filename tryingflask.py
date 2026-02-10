from flask import Flask, redirect, url_for, request, render_template
import os
app = Flask(__name__)

basedir = os.path.abspath(os.path.dirname(__file__))
data_file = os.path.join(basedir, 'static/txt/all_majors.txt')

majors = []
with open(data_file, "r") as file:
    for line in file.readlines():
        majors.append(line)


@app.route('/')
def home():
    # majors = ['Red', 'Blue', 'Black', 'Orange']
    return render_template('login.html', majors=majors)


@app.route('/success/<name>')
def success(name):
    return f'Here is information for major: {name}'


@app.route('/login', methods=['POST', 'GET'])
def login():
    if request.method == 'POST':
        user = request.form['nm']
        return redirect(url_for('success', name=user))
    else:
        user = request.args.get('nm')
        return redirect(url_for('success', name=user))


if __name__ == '__main__':
    app.run(debug=True)
